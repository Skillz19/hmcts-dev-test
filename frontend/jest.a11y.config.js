module.exports = {
  roots: ['<rootDir>/src/test'],
  testMatch: ['**/a11y/**/*.(test|spec).(ts|js)'],
  moduleFileExtensions: ['ts', 'js', 'json'],
  testEnvironment: 'node',
  transform: {
    '^.+\\.ts?$': 'ts-jest',
  },
};
