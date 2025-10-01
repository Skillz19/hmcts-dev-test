# Stage 1: Build
FROM node:20 AS build

WORKDIR /app

# Copy package files
COPY package.json yarn.lock ./

# Enable Corepack and install dependencies with PnP
RUN corepack enable
RUN corepack prepare yarn@3.8.2 --activate
RUN yarn install --immutable

# Copy source code
COPY . .

# Build TypeScript and frontend assets
RUN yarn tsc
RUN yarn build:prod

# Stage 2: Production image
FROM node:20-slim

WORKDIR /app

# Enable Corepack and activate Yarn 3.8.2
RUN corepack enable
RUN corepack prepare yarn@3.8.2 --activate

# Copy Yarn PnP files
COPY --from=build /app/.pnp.cjs ./
COPY --from=build /app/.yarn ./.yarn
COPY --from=build /app/package.json ./
COPY --from=build /app/yarn.lock ./

# Copy compiled code and assets
COPY --from=build /app/dist ./dist

# Expose port
EXPOSE 3100

# Start server
CMD ["yarn", "node", "dist/main/server.js"]
