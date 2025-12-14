# Use official Node.js image for building
FROM node:25-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Production image
FROM node:25-alpine
WORKDIR /app

# Install curl for health checks
RUN apk add --no-cache curl

COPY --from=builder /app .
EXPOSE 3000
CMD ["npm", "start"]

