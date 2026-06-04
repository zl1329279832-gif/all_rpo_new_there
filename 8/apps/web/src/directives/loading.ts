import type { Directive, DirectiveBinding } from 'vue';
import { ElLoading } from 'element-plus';

const loadingInstanceMap = new WeakMap<HTMLElement, any>();

export const loadingDirective: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding<boolean>) {
    if (binding.value) {
      const instance = ElLoading.service({
        target: el,
        lock: true,
        text: '加载中...',
        background: 'rgba(255, 255, 255, 0.7)',
      });
      loadingInstanceMap.set(el, instance);
    }
  },

  updated(el: HTMLElement, binding: DirectiveBinding<boolean>) {
    const instance = loadingInstanceMap.get(el);

    if (binding.value && !instance) {
      const newInstance = ElLoading.service({
        target: el,
        lock: true,
        text: '加载中...',
        background: 'rgba(255, 255, 255, 0.7)',
      });
      loadingInstanceMap.set(el, newInstance);
    } else if (!binding.value && instance) {
      instance.close();
      loadingInstanceMap.delete(el);
    }
  },

  unmounted(el: HTMLElement) {
    const instance = loadingInstanceMap.get(el);
    if (instance) {
      instance.close();
      loadingInstanceMap.delete(el);
    }
  },
};
