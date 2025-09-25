import { configurationRepository } from '../repositories/configurationRepository.js';

export const configurationService = {
  async updateConfiguration(entries) {
    const promises = Object.entries(entries).map(([key, value]) =>
      configurationRepository.upsert(key, JSON.stringify(value))
    );
    return Promise.all(promises);
  },

  async getConfiguration() {
    const records = await configurationRepository.list();
    return records.reduce((acc, record) => {
      acc[record.clave] = JSON.parse(record.valor);
      return acc;
    }, {});
  }
};
