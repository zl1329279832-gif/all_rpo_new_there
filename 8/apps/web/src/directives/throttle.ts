import type { Directive, DirectiveBinding } from 'vue';
import { throttle } from '@platform/shared-utils';

interface ThrottleHTMLElement extends HTMLElement {
  __throttle_handler__?: (e: Event) => void;
}

export const throttleDirective: Directive = {
  mounted(el: ThrottleHTMLElement, binding: DirectiveBinding<{ handler: (e: Event) => void; delay?: number; event?: string }>) {
    const { handler, delay = 300, event = 'click' } = binding.value;
    const throttledHandler = throttle(handler, delay);
    el.addEventListener(event, throttledHandler);
    el.__throttle_handler__ = throttledHandler;
  },

  beforeUnmount(el: ThrottleHTMLElement, binding: DirectiveBinding<{ handler: (e: Event) => void; delay?: number; event?: string }>) {
    const { event = 'click' } = binding.value;
    if (el.__throttle_handler__) {
      el.removeEventListener(event, el.__throttle_handler__);
    }
  },
};
