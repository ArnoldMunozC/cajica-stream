# Documentación de Almacenamiento de Videos

## Configuración Actual

Los videos de la plataforma Cajica Stream se almacenan en una ubicación externa al proyecto:

- **Ubicación de videos**: `/Users/arnmunoz/Documents/Learning/videos`
- **Ubicación de imágenes/miniaturas**: `./uploads/images` (dentro del proyecto)

## Historial de Cambios

- **Agosto 2025**: Se modificó la ubicación de almacenamiento de videos para utilizar una carpeta externa al proyecto.
- Videos subidos antes de este cambio se encuentran en: `./uploads/videos`
- Videos nuevos se almacenan en: `/Users/arnmunoz/Documents/Learning/videos`

## Estructura de Archivos

Los archivos de video se almacenan con nombres generados automáticamente (UUID) para evitar colisiones de nombres. El formato es:
`{uuid}.mp4` (ejemplo: `b9194865-0ca1-4f8b-9cb2-54a036b35b52.mp4`)

## Configuración Técnica

La configuración de almacenamiento se define en el archivo `application.properties`:

```properties
# Configuración para almacenamiento de archivos
app.upload.dir=./uploads/images
app.upload.video.dir=/Users/arnmunoz/Documents/Learning/videos
```

El servicio `FileStorageService` es responsable de gestionar el almacenamiento de archivos, creando las carpetas necesarias si no existen.

## Consideraciones para Despliegue

Al desplegar en un nuevo entorno, asegúrese de:

1. Crear la carpeta externa para videos si no existe
2. Actualizar la propiedad `app.upload.video.dir` en `application.properties` con la ruta correcta
3. Asegurar que la aplicación tenga permisos de escritura en la carpeta externa

## Acceso a Videos Existentes

Los videos subidos antes del cambio de ubicación seguirán siendo accesibles desde su ubicación original. La aplicación gestiona automáticamente la ubicación correcta de cada video según su fecha de subida.
