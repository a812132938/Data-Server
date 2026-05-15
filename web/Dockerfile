FROM node:20-alpine AS build

WORKDIR /workspace

ARG NPM_REGISTRY=https://registry.npmmirror.com

COPY package*.json ./
RUN npm config set registry ${NPM_REGISTRY} && npm ci

COPY . .
RUN npx vite build

FROM nginx:1.27-alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /workspace/dist /usr/share/nginx/html

EXPOSE 80
