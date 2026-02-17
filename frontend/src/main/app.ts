import * as path from 'path';

import { HTTPError } from './HttpError';
import { Nunjucks } from './modules/nunjucks';

import * as bodyParser from 'body-parser';
import cookieParser from 'cookie-parser';
import csurf from 'csurf';
import dotenv from 'dotenv';
dotenv.config();
import express from 'express';
import { glob } from 'glob';
import favicon from 'serve-favicon';

const env = process.env.NODE_ENV || 'development';
const developmentMode = env === 'development';

export const app = express();
app.locals.ENV = env;

new Nunjucks(developmentMode).enableFor(app);

app.use(favicon(path.join(__dirname, '/public/assets/images/favicon.ico')));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));
app.use(cookieParser());
app.use(
  csurf({
    cookie: {
      httpOnly: true,
      sameSite: 'lax',
      secure: !developmentMode,
    },
  })
);
app.use((req, res, next) => {
  res.locals.csrfToken = req.csrfToken();
  next();
});
app.use(express.static(path.join(__dirname, 'public')));
app.use((req, res, next) => {
  res.setHeader('Cache-Control', 'no-cache, max-age=0, must-revalidate, no-store');
  next();
});

// Redirect root to /tasks
app.get('/', (req, res) => {
  res.redirect('/tasks');
});

glob
  .sync(__dirname + '/routes/**/*.+(ts|js)')
  .map(filename => require(filename))
  .forEach(route => {
    if (typeof route.default === 'function') {
      route.default(app);
    }
  });

if (developmentMode) {
  const { setupDev } = require('./development');
  setupDev(app, developmentMode);
}

app.use((err: Error & { code?: string }, req: express.Request, res: express.Response, next: express.NextFunction) => {
  if (err.code !== 'EBADCSRFTOKEN') {
    next(err);
    return;
  }

  res.status(403);
  res.render('error', { message: 'Invalid CSRF token' });
});

// error handler
app.use((err: HTTPError, req: express.Request, res: express.Response) => {
  // set locals, only providing error in development
  res.locals.message = err.message;
  res.locals.error = env === 'development' ? err : {};
  res.status(err.status || 500);
  res.render('error');

  return;
});
