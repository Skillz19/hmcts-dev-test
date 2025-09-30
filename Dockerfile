FROM node:20

# Enable Corepack to use Yarn 3
RUN corepack enable

WORKDIR /app

COPY hmcts-dev-test-frontend/package.json hmcts-dev-test-frontend/yarn.lock ./

# Use Yarn 3 to install dependencies
RUN corepack prepare yarn@3.8.2 --activate
RUN yarn install

COPY hmcts-dev-test-frontend/ ./

RUN yarn webpack --mode production

EXPOSE 3000

CMD ["yarn", "start"]
