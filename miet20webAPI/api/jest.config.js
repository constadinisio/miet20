export default {
  testEnvironment: 'node',
  collectCoverageFrom: ['src/**/*.js', '!src/server.js'],
  coverageDirectory: 'coverage',
  transform: {},
  moduleFileExtensions: ['js', 'json']
};
