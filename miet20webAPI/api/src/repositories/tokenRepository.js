import { getDatabase } from '../config/database.js';
import { insertAndFetch, updateAndFetch, upsertOnConflict } from '../utils/dbHelpers.js';
import { parseDate } from '../utils/tokenUtils.js';

const REFRESH_TABLE = 'auth_refresh_tokens';
const BLACKLIST_TABLE = 'auth_revoked_tokens';

const mapRefreshRecord = (record) => {
  if (!record) {
    return null;
  }

  return {
    id: record.id,
    userId: record.user_id,
    tokenHash: record.token_hash,
    sessionId: record.session_id,
    expiresAt: parseDate(record.expires_at),
    revokedAt: parseDate(record.revoked_at),
    revokeReason: record.revoke_reason || null,
    metadata: (() => {
      if (!record.metadata) {
        return null;
      }
      try {
        return typeof record.metadata === 'string' ? JSON.parse(record.metadata) : record.metadata;
      } catch (error) {
        return null;
      }
    })()
  };
};

export const tokenRepository = {
  async storeRefreshToken({ userId, tokenHash, sessionId, expiresAt, metadata = null }) {
    const payload = {
      user_id: userId,
      token_hash: tokenHash,
      session_id: sessionId,
      expires_at: expiresAt,
      metadata: metadata ? JSON.stringify(metadata) : null,
      revoked_at: null,
      revoke_reason: null
    };

    const created = await insertAndFetch(REFRESH_TABLE, payload, {
      primaryKey: 'id'
    });
    return mapRefreshRecord(created);
  },

  async findRefreshToken(tokenHash) {
    const record = await getDatabase()(REFRESH_TABLE).where({ token_hash: tokenHash }).first();
    return mapRefreshRecord(record);
  },

  async revokeRefreshToken(tokenHash, userId, reason = 'revoked') {
    const criteria = userId ? { token_hash: tokenHash, user_id: userId } : { token_hash: tokenHash };
    const updated = await updateAndFetch(REFRESH_TABLE, criteria, {
      revoked_at: new Date(),
      revoke_reason: reason
    }, {
      primaryKey: 'token_hash'
    });
    return mapRefreshRecord(updated);
  },

  async revokeUserRefreshTokens(userId, reason = 'global_logout') {
    return getDatabase()(REFRESH_TABLE)
      .where({ user_id: userId })
      .whereNull('revoked_at')
      .update({ revoked_at: new Date(), revoke_reason: reason });
  },

  async addAccessTokenToBlacklist({ tokenHash, userId, expiresAt }) {
    const payload = {
      token_hash: tokenHash,
      user_id: userId,
      expires_at: expiresAt,
      created_at: new Date(),
      updated_at: new Date()
    };

    const record = await upsertOnConflict(
      BLACKLIST_TABLE,
      payload,
      ['token_hash'],
      {
        user_id: userId,
        expires_at: expiresAt,
        updated_at: new Date()
      }
    );

    return {
      tokenHash: record.token_hash,
      userId: record.user_id,
      expiresAt: parseDate(record.expires_at)
    };
  },

  async isAccessTokenBlacklisted(tokenHash) {
    const record = await getDatabase()(BLACKLIST_TABLE).where({ token_hash: tokenHash }).first();
    if (!record) {
      return false;
    }

    const expiresAt = parseDate(record.expires_at);
    if (expiresAt && expiresAt < new Date()) {
      return false;
    }

    return true;
  },

  async purgeExpiredBlacklist(referenceDate = new Date()) {
    return getDatabase()(BLACKLIST_TABLE).where('expires_at', '<', referenceDate).del();
  }
};
