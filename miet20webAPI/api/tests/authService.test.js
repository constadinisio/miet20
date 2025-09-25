import { jest } from '@jest/globals';
import bcrypt from 'bcryptjs';
import jwt from 'jsonwebtoken';
import { hashToken } from '../src/utils/tokenUtils.js';

process.env.JWT_SECRET = 'super-secret';
process.env.JWT_EXPIRATION = '1h';
process.env.REFRESH_TOKEN_SECRET = 'refresh-secret';
process.env.REFRESH_TOKEN_EXPIRATION = '2h';
process.env.GOOGLE_CLIENT_ID = 'test-google-client';

const mockUserRepository = {
  findByEmail: jest.fn(),
  findById: jest.fn()
};

const mockRoleRepository = {
  findById: jest.fn(),
  findForUser: jest.fn()
};

const mockTokenRepository = {
  storeRefreshToken: jest.fn(),
  findRefreshToken: jest.fn(),
  revokeRefreshToken: jest.fn(),
  revokeUserRefreshTokens: jest.fn(),
  addAccessTokenToBlacklist: jest.fn()
};

const mockGoogleClient = {
  verifyIdToken: jest.fn()
};

jest.unstable_mockModule('../src/repositories/userRepository.js', () => ({
  userRepository: mockUserRepository
}));

jest.unstable_mockModule('../src/repositories/roleRepository.js', () => ({
  roleRepository: mockRoleRepository
}));

jest.unstable_mockModule('../src/repositories/tokenRepository.js', () => ({
  tokenRepository: mockTokenRepository
}));

jest.unstable_mockModule('google-auth-library', () => ({
  OAuth2Client: jest.fn().mockImplementation(() => mockGoogleClient)
}));

const { authService } = await import('../src/services/authService.js');
const { ApiError } = await import('../src/utils/ApiError.js');

const buildUser = async () => ({
  id: 1,
  mail: 'profesor@et20.edu',
  contrasena: await bcrypt.hash('Secreta123', 8),
  rol: 3,
  status: 1,
  permNoticia: 0,
  permSubidaArch: 0
});

describe('authService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockRoleRepository.findById.mockResolvedValue({ id: 3, nombre: 'Profesor' });
    mockRoleRepository.findForUser.mockResolvedValue([]);
    mockGoogleClient.verifyIdToken.mockResolvedValue({
      getPayload: () => ({ email: 'profesor@et20.edu', email_verified: true })
    });
  });

  test('login emite tokens y persiste refresh token', async () => {
    const user = await buildUser();
    mockUserRepository.findByEmail.mockResolvedValue(user);
    mockTokenRepository.storeRefreshToken.mockResolvedValue({ id: 10, tokenHash: 'hash' });

    const result = await authService.login({ email: 'profesor@et20.edu', password: 'Secreta123' });

    expect(result.tokens.accessToken).toBeDefined();
    expect(result.tokens.refreshToken).toBeDefined();
    expect(mockTokenRepository.storeRefreshToken).toHaveBeenCalledWith(
      expect.objectContaining({ userId: user.id, tokenHash: expect.any(String) })
    );
  });

  test('refreshToken rota el token vigente y conserva la sesión', async () => {
    const user = await buildUser();
    mockUserRepository.findByEmail.mockResolvedValue(user);
    mockUserRepository.findById.mockResolvedValue(user);

    let storedHash;
    let initialHash;
    mockTokenRepository.storeRefreshToken.mockImplementation(async (payload) => {
      storedHash = payload.tokenHash;
      if (!initialHash) {
        initialHash = payload.tokenHash;
      }
      return { id: 99, userId: user.id, tokenHash: payload.tokenHash, expiresAt: new Date(Date.now() + 60_000) };
    });

    const { tokens } = await authService.login({ email: user.mail, password: 'Secreta123' });

    mockTokenRepository.findRefreshToken.mockResolvedValue({
      id: 99,
      userId: user.id,
      tokenHash: initialHash,
      expiresAt: new Date(Date.now() + 60_000)
    });
    mockTokenRepository.revokeRefreshToken.mockResolvedValue({});

    const rotated = await authService.refreshToken(tokens.refreshToken);

    expect(rotated.tokens).toBeDefined();
    expect(mockTokenRepository.revokeRefreshToken).toHaveBeenCalledWith(initialHash, user.id, 'rotated');
    expect(mockTokenRepository.storeRefreshToken).toHaveBeenCalledTimes(2);
    expect(storedHash).not.toEqual(initialHash);
  });

  test('logout revoca tokens y agrega el access token a la blacklist', async () => {
    const user = await buildUser();
    mockUserRepository.findByEmail.mockResolvedValue(user);
    mockUserRepository.findById.mockResolvedValue(user);

    let storedHash;
    mockTokenRepository.storeRefreshToken.mockImplementation(async (payload) => {
      storedHash = payload.tokenHash;
      return { id: 123, userId: user.id, tokenHash: payload.tokenHash, expiresAt: new Date(Date.now() + 120_000) };
    });

    const { tokens } = await authService.login({ email: user.mail, password: 'Secreta123' });

    mockTokenRepository.findRefreshToken.mockResolvedValue({
      id: 123,
      userId: user.id,
      tokenHash: storedHash,
      expiresAt: new Date(Date.now() + 120_000)
    });

    await authService.logout(user.id, { refreshToken: tokens.refreshToken, accessToken: tokens.accessToken });

    expect(mockTokenRepository.revokeRefreshToken).toHaveBeenCalledWith(storedHash, user.id, 'logout');
    expect(mockTokenRepository.addAccessTokenToBlacklist).toHaveBeenCalledWith(
      expect.objectContaining({ tokenHash: hashToken(tokens.accessToken), userId: user.id })
    );
  });

  test('loginWithGoogle valida el token recibido', async () => {
    const user = await buildUser();
    mockUserRepository.findByEmail.mockResolvedValue(user);

    const result = await authService.loginWithGoogle('token-google');

    expect(result.user.email).toBe(user.mail);
    expect(mockGoogleClient.verifyIdToken).toHaveBeenCalledWith(
      expect.objectContaining({ idToken: 'token-google' })
    );
  });

  test('refreshToken lanza error cuando el token está revocado', async () => {
    mockTokenRepository.findRefreshToken.mockResolvedValue({
      id: 1,
      userId: 99,
      tokenHash: 'hash',
      revokedAt: new Date()
    });

    await expect(authService.refreshToken(jwt.sign({ id: 1 }, 'refresh-secret'))).rejects.toThrow(ApiError);
  });
});
