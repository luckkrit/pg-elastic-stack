// src/server/db.ts
import postgres from 'postgres'

export const sql = postgres(process.env.DATABASE_URL || 'postgresql://postgres:password@localhost:5432/postgres', {
  max: 10,            // ขนาด pool
  idle_timeout: 20,
})