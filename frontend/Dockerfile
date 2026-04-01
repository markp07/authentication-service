# Use official Node.js image for building
FROM node:25-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .

# Build with placeholder values - will be replaced at runtime
ARG NEXT_PUBLIC_API_URL=https://auth.yourdomain.tld
ARG NEXT_PUBLIC_BASE_DOMAIN=yourdomain.tld
ARG NEXT_PUBLIC_SUPPORT_EMAIL=support@yourdomain.tld
ARG NEXT_PUBLIC_EXTRA_MENU_ITEMS=

RUN npm run build

# Production image
FROM node:25-alpine
WORKDIR /app

# Install curl for health checks
RUN apk add --no-cache curl

COPY --from=builder /app .
EXPOSE 3000

# The environment variables will be read at runtime from docker-compose
CMD ["npm", "start"]
