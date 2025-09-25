export const STUDENT_ROLE_ID = 4;

export const COMMON_REQUIRED_FIELDS = {
  nombre: 'Nombre',
  apellido: 'Apellido',
  mail: 'Correo electrónico',
  dni: 'DNI',
  telefono: 'Teléfono',
  direccion: 'Dirección',
  fecha_nacimiento: 'Fecha de nacimiento'
};

export const NON_STUDENT_EXTRA_FIELDS = {
  ficha_censal: 'Ficha censal'
};

const isEmptyValue = (value) => {
  if (value === null || value === undefined) {
    return true;
  }

  if (typeof value === 'string') {
    const trimmed = value.trim();
    return trimmed === '' || trimmed === '0000-00-00';
  }

  return false;
};

const normalizeRoleId = (roleId) => {
  const numeric = Number(roleId);
  return Number.isFinite(numeric) ? numeric : null;
};

export const buildRequiredFieldsResponse = (roleId) => {
  const normalizedRole = normalizeRoleId(roleId);
  const includeExtra = normalizedRole === null || normalizedRole !== STUDENT_ROLE_ID;

  return {
    roleId: normalizedRole,
    common: { ...COMMON_REQUIRED_FIELDS },
    extra: includeExtra ? { ...NON_STUDENT_EXTRA_FIELDS } : {},
    fields: {
      ...COMMON_REQUIRED_FIELDS,
      ...(includeExtra ? NON_STUDENT_EXTRA_FIELDS : {})
    }
  };
};

export const resolveMissingRequiredFields = (user, roleId) => {
  const response = buildRequiredFieldsResponse(roleId);
  const missing = {};

  Object.entries(response.fields).forEach(([field, label]) => {
    if (isEmptyValue(user?.[field])) {
      missing[field] = label;
    }
  });

  return {
    ...response,
    missing
  };
};
