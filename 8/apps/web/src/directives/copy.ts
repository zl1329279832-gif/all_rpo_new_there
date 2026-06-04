import type { Directive, DirectiveBinding } from 'vue';
import { ElMessage } from 'element-plus';

export const copyDirective: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding<string>) {
    el.addEventListener('click', async () => {
      const value = binding.value || el.textContent;
      if (!value) {
        ElMessage.warning('没有可复制的内容');
        return;
      }

      try {
        await navigator.clipboard.writeText(value);
        ElMessage.success('复制成功');
      } catch (error) {
        const textarea = document.createElement('textarea');
        textarea.value = value;
        textarea.style.position = 'fixed';
        textarea.style.opacity = '0';
        document.body.appendChild(textarea);
        textarea.select();
        document.execCommand('copy');
        document.body.removeChild(textarea);
        ElMessage.success('复制成功');
      }
    });
  },

  updated(el: HTMLElement, binding: DirectiveBinding<string>) {
    (el as any).__copyValue = binding.value;
  },
};
