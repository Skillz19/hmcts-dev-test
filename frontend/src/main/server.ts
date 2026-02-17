#!/usr/bin/env node
import * as fs from 'fs';
import * as https from 'https';
import * as path from 'path';

import { app } from './app';

let httpsServer: https.Server | null = null;

// used by shutdownCheck in readinessChecks
app.locals.shutdown = false;

// TODO: set the right port for your application
const port: number = parseInt(process.env.PORT || '3100', 10);

if (app.locals.ENV === 'development') {
  const sslDirectory = path.join(__dirname, 'resources', 'localhost-ssl');
  const sslOptions = {
    cert: fs.readFileSync(path.join(sslDirectory, 'localhost.crt')),
    key: fs.readFileSync(path.join(sslDirectory, 'localhost.key')),
  };
  httpsServer = https.createServer(sslOptions, app);
  httpsServer.listen(port, () => {
    process.stdout.write(`Application started: https://localhost:${port}\n`);
  });
} else {
  app.listen(port, () => {
    process.stdout.write(`Application started: http://localhost:${port}\n`);
  });
}

function gracefulShutdownHandler(signal: string) {
  process.stdout.write(`⚠️ Caught ${signal}, gracefully shutting down. Setting readiness to DOWN\n`);
  // stop the server from accepting new connections
  app.locals.shutdown = true;

  setTimeout(() => {
    process.stdout.write('Shutting down application\n');
    // Close server if it's running
    httpsServer?.close(() => {
      process.stdout.write('HTTPS server closed\n');
    });
  }, 4000);
}

process.on('SIGINT', gracefulShutdownHandler);
process.on('SIGTERM', gracefulShutdownHandler);
