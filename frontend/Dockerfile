FROM node:20-alpine AS builder
WORKDIR /app
COPY package.json .
RUN npm install --registry=https://registry.npmmirror.com
COPY . .
RUN npm run build

FROM nginx:1.25-alpine
RUN apk add --no-cache tzdata openssl
ENV TZ=Asia/Shanghai

# Generate default self-signed certificate
RUN mkdir -p /etc/nginx/certs && \
    openssl req -x509 -nodes -days 3650 \
    -newkey rsa:2048 \
    -keyout /etc/nginx/certs/nginx.key \
    -out /etc/nginx/certs/nginx.crt \
    -subj "/C=CN/ST=Local/L=Local/O=RAGFlow/CN=localhost"

# Copy nginx snippets
RUN mkdir -p /etc/nginx/snippets
COPY nginx-snippets/app.conf /etc/nginx/snippets/app.conf

COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80 443
