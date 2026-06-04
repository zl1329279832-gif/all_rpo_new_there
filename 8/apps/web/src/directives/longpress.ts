import type { Directive, DirectiveBinding } from 'vue';

interface LongpressHTMLElement extends HTMLElement {
  __longpress_timer__?: NodeJS.Timeout;
  __longpress_start__?: number;
}

export const longpressDirective: Directive = {
  mounted(el: LongpressHTMLElement, binding: DirectiveBinding<(e: Event) => void>) {
    const duration = 500;

    const start = (e: Event) => {
      el.__longpress_start__ = Date.now();
      el.__longpress_timer__ = setTimeout(() => {
        binding.value(e);
      }, duration);
    };

    const cancel = () => {
      if (el.__longpress_timer__) {
        clearTimeout(el.__longpress_timer__);
        el.__longpress_timer__ = undefined;
      }
    };

    el.addEventListener('mousedown', start);
    el.addEventListener('touchstart', start);
    el.addEventListener('click', cancel);
    el.addEventListener('mouseout', cancel);
    el.addEventListener('touchend', cancel);
    el.addEventListener('touchcancel', cancel);

    (el as any).__longpress_handlers__ = { start, cancel };
  },

  unmounted(el: LongpressHTMLElement) {
    const handlers = (el as any).__longpress_handlers__;
    if (handlers) {
      el.removeEventListener('mousedown', handlers.start);
      el.removeEventListener('touchstart', handlers.start);
      el.removeEventListener('click', handlers.cancel);
      el.removeEventListener('mouseout', handlers.cancel);
      el.removeEventListener('touchend', handlers.cancel);
      el.removeEventListener('touchcancel', handlers.cancel);
    }
    if (el.__longpress_timer__) {
      clearTimeout(el.__longpress_timer__);
    }
  },
};
