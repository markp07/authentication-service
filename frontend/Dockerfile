# Use official Node.js image for building
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Production image
FROM node:20-alpine
WORKDIR /appcd
COPY --from=builder /app .
EXPOSE 3000
CMD ["npm", "start"]

