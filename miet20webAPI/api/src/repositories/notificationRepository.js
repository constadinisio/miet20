import { getDatabase } from '../config/database.js';
import { insertAndFetch, updateAndFetch } from '../utils/dbHelpers.js';

const TABLE_NAME = 'notificaciones';
const GROUPS_BASE_TABLE = 'grupos_notificacion';
const GROUPS_CUSTOM_TABLE = 'grupos_notificacion_personalizados';
const GROUP_MEMBERS_TABLE = 'grupos_notificacion_miembros';
const RECIPIENTS_TABLE = 'notificaciones_destinatarios';
const USERS_TABLE = 'usuarios';
const USER_ROLES_TABLE = 'usuario_roles';
const STUDENT_COURSE_TABLE = 'alumno_curso';
const TEACHER_ASSIGNMENT_TABLE = 'profesor_curso_materia';

const baseNotificationColumns = [
  `${TABLE_NAME}.id`,
  `${TABLE_NAME}.titulo`,
  `${TABLE_NAME}.contenido`,
  `${TABLE_NAME}.tipo_notificacion`,
  `${TABLE_NAME}.fecha_creacion`,
  `${TABLE_NAME}.prioridad`,
  `${TABLE_NAME}.icono`,
  `${TABLE_NAME}.color`,
  `${TABLE_NAME}.estado`,
  `${TABLE_NAME}.requiere_confirmacion`,
  `${TABLE_NAME}.tipo_especial`,
  `${RECIPIENTS_TABLE}.id as destinatario_row_id`,
  `${RECIPIENTS_TABLE}.fecha_leida`,
  `${RECIPIENTS_TABLE}.fecha_confirmada`,
  `${RECIPIENTS_TABLE}.estado_lectura`
];

export const notificationRepository = {
  async createGroup(group) {
    const db = getDatabase();

    return db.transaction(async (trx) => {
      const baseGroup = await insertAndFetch(
        GROUPS_BASE_TABLE,
        {
          nombre: group.nombre,
          creador_id: group.creador_id ?? null
        },
        { primaryKey: 'id', trx }
      );

      await insertAndFetch(
        GROUPS_CUSTOM_TABLE,
        {
          id: baseGroup.id,
          nombre: group.nombre,
          descripcion: group.descripcion ?? null,
          creador_id: group.creador_id ?? null
        },
        { primaryKey: 'id', trx }
      );

      const memberIds = Array.isArray(group.miembros)
        ? [...new Set(group.miembros.map((member) => Number(member)).filter((id) => Number.isInteger(id) && id > 0))]
        : [];

      await trx(GROUP_MEMBERS_TABLE).where({ grupo_id: baseGroup.id }).del();

      if (memberIds.length) {
        const rows = memberIds.map((usuarioId) => ({
          grupo_id: baseGroup.id,
          usuario_id: usuarioId,
          activo: 1
        }));

        await trx(GROUP_MEMBERS_TABLE).insert(rows);
      }

      return {
        id: baseGroup.id,
        nombre: group.nombre,
        descripcion: group.descripcion ?? null,
        creador_id: baseGroup.creador_id ?? group.creador_id ?? null,
        miembros: memberIds
      };
    });
  },

  async listGroups() {
    return getDatabase()(GROUPS_CUSTOM_TABLE)
      .leftJoin(`${GROUPS_BASE_TABLE} as gb`, 'gb.id', `${GROUPS_CUSTOM_TABLE}.id`)
      .select(
        `${GROUPS_CUSTOM_TABLE}.id`,
        `${GROUPS_CUSTOM_TABLE}.nombre`,
        `${GROUPS_CUSTOM_TABLE}.descripcion`,
        'gb.creador_id'
      )
      .orderBy(`${GROUPS_CUSTOM_TABLE}.nombre`);
  },

  async replaceGroupMembers(groupId, memberIds, trx = null) {
    const uniqueIds = [...new Set(memberIds.map((value) => Number(value)))].filter(
      (value) => Number.isInteger(value) && value > 0
    );

    const run = async (queryBuilder) => {
      await queryBuilder(GROUP_MEMBERS_TABLE).where({ grupo_id: groupId }).del();

      if (!uniqueIds.length) {
        return;
      }

      const rows = uniqueIds.map((usuarioId) => ({
        grupo_id: groupId,
        usuario_id: usuarioId,
        activo: 1
      }));

      await queryBuilder(GROUP_MEMBERS_TABLE).insert(rows);
    };

    if (trx) {
      await run(trx);
    } else {
      await getDatabase().transaction(run);
    }

    return uniqueIds.length;
  },

  async deleteGroup(groupId) {
    const db = getDatabase();
    let deleted = 0;
    await db.transaction(async (trx) => {
      await trx(GROUP_MEMBERS_TABLE).where({ grupo_id: groupId }).del();
      await trx(GROUPS_CUSTOM_TABLE).where({ id: groupId }).del();
      deleted = await trx(GROUPS_BASE_TABLE).where({ id: groupId }).del();
    });

    return deleted;
  },

  async createNotification(notification) {
    const {
      destinatario_id: destinatarioId,
      destinatario_ids: destinatarioIds,
      destinatarios,
      grupos,
      grupo_id: grupoId,
      mensaje,
      contenido,
      tipo_notificacion: tipoNotificacion,
      prioridad,
      icono,
      color,
      estado,
      requiere_confirmacion: requiereConfirmacion,
      tipo_especial: tipoEspecial,
      remitente_id: remitenteId,
      ...rest
    } = notification;

    const notificationToInsert = {
      titulo: notification.titulo,
      contenido: mensaje ?? contenido ?? '',
      tipo_notificacion: tipoNotificacion ?? 'INDIVIDUAL',
      prioridad: prioridad ?? 'NORMAL',
      icono: icono ?? null,
      color: color ?? null,
      estado: estado ?? 'ACTIVA',
      requiere_confirmacion: requiereConfirmacion ?? false,
      tipo_especial: tipoEspecial ?? null,
      remitente_id: remitenteId ?? null,
      fecha_creacion: rest.fecha_creacion ?? new Date()
    };

    const created = await insertAndFetch(TABLE_NAME, notificationToInsert, { primaryKey: 'id' });

    const individualRecipients = new Set();

    if (Array.isArray(destinatarios)) {
      for (const recipient of destinatarios) {
        if (recipient) {
          individualRecipients.add(Number(recipient));
        }
      }
    }

    if (Array.isArray(destinatarioIds)) {
      for (const recipient of destinatarioIds) {
        if (recipient) {
          individualRecipients.add(Number(recipient));
        }
      }
    }

    if (destinatarioId) {
      individualRecipients.add(Number(destinatarioId));
    }

    const groupIds = [];

    if (Array.isArray(grupos)) {
      for (const group of grupos) {
        const parsed = Number(group);
        if (Number.isInteger(parsed) && parsed > 0) {
          groupIds.push(parsed);
        }
      }
    }

    for (const recipientId of individualRecipients) {
      await insertAndFetch(
        RECIPIENTS_TABLE,
        {
          notificacion_id: created.id,
          destinatario_id: recipientId,
          estado_lectura: 'NO_LEIDA'
        },
        { primaryKey: 'id' }
      );
    }

    const groupsToProcess = [...groupIds];
    if (grupoId) {
      const parsed = Number(grupoId);
      if (Number.isInteger(parsed) && parsed > 0) {
        groupsToProcess.push(parsed);
      }
    }

    if (groupsToProcess.length) {
      const members = await this.listGroupMembers(groupsToProcess);
      for (const member of members) {
        const memberId = member.id ?? member.usuario_id;
        if (!memberId || individualRecipients.has(Number(memberId))) {
          continue;
        }

        await insertAndFetch(
          RECIPIENTS_TABLE,
          {
            notificacion_id: created.id,
            destinatario_id: Number(memberId),
            estado_lectura: 'NO_LEIDA'
          },
          { primaryKey: 'id' }
        );
      }
    }

    return created;
  },

  async listByUser(userId) {
    return getDatabase()(RECIPIENTS_TABLE)
      .join(TABLE_NAME, `${RECIPIENTS_TABLE}.notificacion_id`, '=', `${TABLE_NAME}.id`)
      .select(baseNotificationColumns)
      .where(`${RECIPIENTS_TABLE}.destinatario_id`, userId)
      .andWhere(`${TABLE_NAME}.estado`, 'ACTIVA')
      .orderBy(`${TABLE_NAME}.fecha_creacion`, 'desc');
  },

  async markAsRead(recipientId, userId) {
    return updateAndFetch(
      RECIPIENTS_TABLE,
      { id: recipientId, destinatario_id: userId },
      {
        estado_lectura: 'LEIDA',
        fecha_leida: new Date()
      }
    );
  },

  async confirm(recipientId, userId) {
    return updateAndFetch(
      RECIPIENTS_TABLE,
      { id: recipientId, destinatario_id: userId },
      {
        estado_lectura: 'CONFIRMADA',
        fecha_confirmada: new Date()
      }
    );
  },

  async listGroupMembers(groupIds) {
    const db = getDatabase();
    const ids = Array.isArray(groupIds) ? groupIds : [groupIds];
    const validIds = ids
      .map((value) => Number(value))
      .filter((value) => Number.isInteger(value) && value > 0);

    if (!validIds.length) {
      return [];
    }

    return db(GROUP_MEMBERS_TABLE)
      .join(`${USERS_TABLE} as u`, `${GROUP_MEMBERS_TABLE}.usuario_id`, '=', 'u.id')
      .select('u.id', 'u.nombre', 'u.apellido', 'u.mail', `${GROUP_MEMBERS_TABLE}.grupo_id`)
      .whereIn(`${GROUP_MEMBERS_TABLE}.grupo_id`, validIds)
      .andWhere(`${GROUP_MEMBERS_TABLE}.activo`, 1)
      .andWhere('u.status', 1)
      .orderBy(`${GROUP_MEMBERS_TABLE}.grupo_id`)
      .orderBy('u.apellido')
      .orderBy('u.nombre');
  },

  async listActiveUsers() {
    return getDatabase()(USERS_TABLE).select('id').where('status', 1);
  },

  async listCoursesByTeacher(teacherId) {
    if (!teacherId) {
      return [];
    }

    const db = getDatabase();
    return db(`${TEACHER_ASSIGNMENT_TABLE} as pcm`)
      .join('cursos as c', 'c.id', 'pcm.curso_id')
      .where('pcm.profesor_id', teacherId)
      .groupBy('c.id', 'c.anio', 'c.division', 'c.turno')
      .orderBy('c.anio')
      .orderBy('c.division')
      .select('c.id', 'c.anio', 'c.division', 'c.turno');
  },

  async listStudentsDetailByCourseIds(courseIds) {
    const ids = Array.isArray(courseIds) ? courseIds : [courseIds];
    const validIds = ids
      .map((value) => Number(value))
      .filter((value) => Number.isInteger(value) && value > 0);

    if (!validIds.length) {
      return [];
    }

    return getDatabase()(`${STUDENT_COURSE_TABLE} as ac`)
      .join(`${USERS_TABLE} as u`, 'u.id', '=', 'ac.alumno_id')
      .select('u.id', 'u.nombre', 'u.apellido', 'ac.curso_id')
      .whereIn('ac.curso_id', validIds)
      .andWhere('u.status', 1)
      .orderBy('u.apellido')
      .orderBy('u.nombre');
  },

  async listUsersByRoleIds(roleIds) {
    const ids = Array.isArray(roleIds) ? roleIds : [roleIds];
    const validIds = ids
      .map((value) => Number(value))
      .filter((value) => Number.isInteger(value) && value > 0);

    if (!validIds.length) {
      return [];
    }

    const db = getDatabase();

    const primaryRoles = await db(USERS_TABLE)
      .select('id')
      .whereIn('rol', validIds)
      .andWhere('status', 1);

    const additionalRoles = await db(USER_ROLES_TABLE)
      .join(`${USERS_TABLE} as u`, 'u.id', '=', `${USER_ROLES_TABLE}.usuario_id`)
      .select('u.id')
      .whereIn(`${USER_ROLES_TABLE}.rol_id`, validIds)
      .andWhere('u.status', 1);

    const merged = new Map();
    primaryRoles.forEach((user) => {
      if (user?.id) {
        merged.set(Number(user.id), { id: Number(user.id) });
      }
    });
    additionalRoles.forEach((user) => {
      if (user?.id) {
        merged.set(Number(user.id), { id: Number(user.id) });
      }
    });

    return Array.from(merged.values());
  },

  async listStudentsByCourseIds(courseIds) {
    const ids = Array.isArray(courseIds) ? courseIds : [courseIds];
    const validIds = ids
      .map((value) => Number(value))
      .filter((value) => Number.isInteger(value) && value > 0);

    if (!validIds.length) {
      return [];
    }

    return getDatabase()(STUDENT_COURSE_TABLE)
      .join(`${USERS_TABLE} as u`, `${STUDENT_COURSE_TABLE}.alumno_id`, '=', 'u.id')
      .select(`${STUDENT_COURSE_TABLE}.alumno_id as id`)
      .whereIn(`${STUDENT_COURSE_TABLE}.curso_id`, validIds)
      .andWhere('u.status', 1);
  }
};
