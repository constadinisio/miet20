import { roleRepository } from '../repositories/roleRepository.js';

export const roleService = {
  listRoles() {
    return roleRepository.listAll();
  }
};
