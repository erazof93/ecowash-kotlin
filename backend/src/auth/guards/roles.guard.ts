// src/auth/guards/roles.guard.ts
import { Injectable, CanActivate, ExecutionContext, ForbiddenException, SetMetadata } from '@nestjs/common';
import { Reflector } from '@nestjs/core';
import { rol_enum } from '@prisma/client';

// 🏷️ PASO 2 EN MIGRACIÓN: El decorador se queda aquí mismo
export const ROLES_KEY = 'roles';
export const Roles = (...roles: rol_enum[]) => SetMetadata(ROLES_KEY, roles);

@Injectable()
export class RolesGuard implements CanActivate {
  constructor(private reflector: Reflector) {}

  canActivate(context: ExecutionContext): boolean {
    // 1. Extraemos los roles requeridos usando la llave local ROLES_KEY
    const requeridosRoles = this.reflector.getAllAndOverride<rol_enum[]>(ROLES_KEY, [
      context.getHandler(),
      context.getClass(),
    ]);

    // Si la ruta no tiene el decorador @Roles, se considera abierta para cualquier usuario logueado
    if (!requeridosRoles) {
      return true;
    }

    // 2. Extraemos el usuario de la petición (inyectado previamente por JwtAuthGuard)
    const { user } = context.switchToHttp().getRequest();

    // 3. Validamos si el rol del usuario está dentro de los permitidos
    const tieneRol = requeridosRoles.includes(user?.rol);

    if (!tieneRol) {
      throw new ForbiddenException('No tienes los permisos necesarios para acceder a este recurso');
    }

    return true;
  }
}