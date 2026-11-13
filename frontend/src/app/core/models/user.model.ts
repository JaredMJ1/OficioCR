export type UserRole = 'usuario' | 'empleador' | 'empleado' | 'operador' | 'superusuario';
export interface User { id: string; name: string; email: string; role: UserRole; }
export interface AuthResponse { expiresAt: string; user: User; }
