package com.jrdm.Kando.domain.enums;

public enum BoardRole {
    OWNER,   // Creador del tablero, permisos totales
    ADMIN,   // Puede gestionar miembros y tareas
    MEMBER,  // Puede crear y editar tareas
    VIEWER   // Solo lectura
}
