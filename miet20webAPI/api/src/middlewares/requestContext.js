import { randomUUID } from 'crypto';

export const requestContext = (req, res, next) => {
  req.id = randomUUID();
  res.setHeader('X-Request-Id', req.id);
  next();
};
