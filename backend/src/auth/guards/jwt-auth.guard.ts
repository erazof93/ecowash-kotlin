// src/auth/guards/jwt-auth.guard.ts
import { Injectable } from '@nestjs/common';
import { AuthGuard } from '@nestjs/passport';
import { JWT_STRATEGY_NAME } from '../strategies/jwt.strategy';

@Injectable()
// Le decimos que use la estrategia 'jwt' que tú ya programaste arriba
export class JwtAuthGuard extends AuthGuard(JWT_STRATEGY_NAME) {}