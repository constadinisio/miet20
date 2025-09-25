import { getDatabase } from '../config/database.js';

const getClientName = () => getDatabase().client.config.client;

const isMySQLFamily = () => {
  const client = getClientName();
  return client.includes('mysql') || client.includes('maria');
};

export const insertAndFetch = async (table, payload, { primaryKey = 'id', whereColumns = [], trx = null } = {}) => {
  const db = trx ?? getDatabase();

  if (!isMySQLFamily()) {
    const [created] = await db(table).insert(payload).returning('*');
    return created;
  }

  const insertResult = await db(table).insert(payload);
  let criteria = null;

  if (whereColumns.length) {
    criteria = whereColumns.reduce((acc, column) => ({
      ...acc,
      [column]: payload[column]
    }), {});
  } else if (primaryKey) {
    const insertedId = Array.isArray(insertResult) ? insertResult[0] : insertResult;
    criteria = { [primaryKey]: insertedId };
  }

  if (!criteria) {
    return { ...payload };
  }

  return db(table).where(criteria).first();
};

export const updateAndFetch = async (table, identifier, updates, { primaryKey = 'id', trx = null } = {}) => {
  const db = trx ?? getDatabase();
  const criteria = typeof identifier === 'object' ? identifier : { [primaryKey]: identifier };

  const affected = await db(table).where(criteria).update(updates);
  if (!affected) {
    return null;
  }

  return db(table).where(criteria).first();
};

export const upsertOnConflict = async (table, payload, conflictColumns, mergeValues) => {
  const db = getDatabase();
  const criteria = conflictColumns.reduce((acc, column) => ({
    ...acc,
    [column]: payload[column]
  }), {});

  if (!isMySQLFamily()) {
    const [record] = await db(table)
      .insert(payload)
      .onConflict(conflictColumns)
      .merge(mergeValues)
      .returning('*');
    return record;
  }

  const updated = await db(table).where(criteria).update(mergeValues);
  if (!updated) {
    await db(table).insert(payload);
  }

  return db(table).where(criteria).first();
};

export const isMySQLClient = () => isMySQLFamily();
