import type { App } from 'vue';
import { permissionDirective } from './permission';
import { loadingDirective } from './loading';
import { copyDirective } from './copy';
import { longpressDirective } from './longpress';
import { debounceDirective } from './debounce';
import { throttleDirective } from './throttle';
import { dragDirective } from './drag';

export function setupDirectives(app: App): void {
  app.directive('permission', permissionDirective);
  app.directive('loading', loadingDirective);
  app.directive('copy', copyDirective);
  app.directive('longpress', longpressDirective);
  app.directive('debounce', debounceDirective);
  app.directive('throttle', throttleDirective);
  app.directive('drag', dragDirective);
}

export * from './permission';
export * from './loading';
export * from './copy';
export * from './longpress';
export * from './debounce';
export * from './throttle';
export * from './drag';
