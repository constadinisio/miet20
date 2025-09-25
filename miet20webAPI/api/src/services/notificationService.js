import httpStatus from 'http-status';
import { notificationRepository } from '../repositories/notificationRepository.js';
import { ApiError } from '../utils/ApiError.js';

export const notificationService = {
  async createGroup(payload) {
    const members = Array.isArray(payload.miembros)
      ? payload.miembros.map((member) => Number(member)).filter((id) => Number.isInteger(id) && id > 0)
      : [];

    const groupData = {
      nombre: payload.nombre,
      descripcion: payload.descripcion ?? null,
      creador_id: payload.creador_id ? Number(payload.creador_id) : null,
      miembros: members
    };

    return notificationRepository.createGroup(groupData);
  },

  async listGroups() {
    return notificationRepository.listGroups();
  },

  async deleteGroup(groupId) {
    const deleted = await notificationRepository.deleteGroup(groupId);
    if (!deleted) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Grupo no encontrado');
    }
    return true;
  },

  async sendNotification(payload) {
    const tipoNotificacionRaw = payload.tipo ?? payload.tipo_notificacion ?? payload.tipoNotificacion;
    const tipoNotificacion = typeof tipoNotificacionRaw === 'string'
      ? tipoNotificacionRaw.toUpperCase()
      : 'INDIVIDUAL';

    const recipients = new Set();
    const normalizeToArray = (value) => {
      if (Array.isArray(value)) {
        return value;
      }
      if (value === undefined || value === null || value === '') {
        return [];
      }
      return [value];
    };

    const appendRecipients = (values) => {
      for (const value of normalizeToArray(values)) {
        const parsed = Number(value);
        if (Number.isInteger(parsed) && parsed > 0) {
          recipients.add(parsed);
        }
      }
    };

    appendRecipients(payload.destinatarios);
    appendRecipients(payload.destinatario_ids);
    appendRecipients(payload.destinatario_id);
    appendRecipients(payload.recipient_ids);

    const destino = normalizeToArray(payload.destino);

    if (tipoNotificacion === 'ROL') {
      const roles = [...normalizeToArray(payload.roles), ...destino];
      if (roles.length) {
        const users = await notificationRepository.listUsersByRoleIds(roles);
        users.forEach((user) => appendRecipients(user?.id));
      }
    }

    if (tipoNotificacion === 'GRUPO') {
      const grupos = [
        ...normalizeToArray(payload.grupos),
        ...normalizeToArray(payload.grupo_id),
        ...destino
      ];

      if (grupos.length) {
        const members = await notificationRepository.listGroupMembers(grupos);
        members.forEach((member) => appendRecipients(member?.id ?? member?.usuario_id));
      }
    }

    if (tipoNotificacion === 'GLOBAL') {
      const users = await notificationRepository.listActiveUsers();
      users.forEach((user) => appendRecipients(user?.id));
    }

    if (tipoNotificacion === 'CURSO') {
      const cursos = [...normalizeToArray(payload.cursos), ...destino];
      if (cursos.length) {
        const students = await notificationRepository.listStudentsByCourseIds(cursos);
        students.forEach((student) => appendRecipients(student?.id));
      }
    }

    if (!recipients.size) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'No se encontraron destinatarios para la notificación');
    }

    const requiereConfirmacion =
      payload.requiere_confirmacion ?? payload.requiereConfirmacion ?? false;

    return notificationRepository.createNotification({
      ...payload,
      contenido: payload.contenido ?? payload.mensaje ?? '',
      mensaje: payload.mensaje ?? payload.contenido ?? '',
      tipo_notificacion: tipoNotificacion,
      requiere_confirmacion: requiereConfirmacion,
      destinatario_ids: Array.from(recipients)
    });
  },

  async listNotifications(userId) {
    const parsedUserId = Number(userId);

    if (!parsedUserId || !Number.isInteger(parsedUserId) || parsedUserId <= 0) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'El usuario autenticado es inválido');
    }

    return notificationRepository.listByUser(parsedUserId);
  },

  async listGroupMembers(groupId) {
    return notificationRepository.listGroupMembers(groupId);
  },

  async listNotificationOptions({ userId, role }) {
    if (!userId) {
      throw new ApiError(httpStatus.BAD_REQUEST, 'El usuario autenticado es inválido');
    }

    if (role !== 'profesor') {
      throw new ApiError(httpStatus.FORBIDDEN, 'El rol autenticado no puede listar estas opciones');
    }

    const teacherId = Number(userId);
    const courses = await notificationRepository.listCoursesByTeacher(teacherId);
    const courseIds = courses.map((course) => course.id);
    const students = courseIds.length
      ? await notificationRepository.listStudentsDetailByCourseIds(courseIds)
      : [];

    return {
      cursos: courses.map((course) => ({
        id: Number(course.id),
        anio: course.anio,
        division: course.division,
        turno: course.turno
      })),
      alumnos: students.map((student) => ({
        id: Number(student.id),
        nombre: student.nombre,
        apellido: student.apellido,
        curso_id: Number(student.curso_id)
      }))
    };
  },

  async markRecipientAsRead(recipientId, userId) {
    const updated = await notificationRepository.markAsRead(recipientId, userId);

    if (!updated) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Notificación no encontrada para el usuario autenticado');
    }

    return updated;
  },

  async confirmRecipient(recipientId, userId) {
    const updated = await notificationRepository.confirm(recipientId, userId);

    if (!updated) {
      throw new ApiError(httpStatus.NOT_FOUND, 'Notificación no encontrada para el usuario autenticado');
    }

    return updated;
  }
};
