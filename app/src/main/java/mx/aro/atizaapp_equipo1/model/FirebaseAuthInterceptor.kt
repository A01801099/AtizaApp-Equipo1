package mx.aro.atizaapp_equipo1.model.apiClientService


//Cuando un usuario inicia sesión con Firebase (ya sea email/password o Google), Firebase Auth mantiene su sesión en el cliente.
//👉 Pero para que tu API backend sepa quién es ese usuario y pueda autorizarlo, debes incluir el ID Token de Firebase (JWT) en cada request HTTP.
//
//Ese ID Token es válido por una hora y luego expira, así que hay que refrescarlo automáticamente cuando el backend devuelve un 401 Unauthorized.

// **Esto maneja automáticamente:**
//- Agregar token a todas las requests
//- Refresh automático en caso de 401
//- Retry automático con nuevo token