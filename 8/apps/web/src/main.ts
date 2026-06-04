import { createApp } from 'vue';
import { createPinia } from 'pinia';
import ElementPlus from 'element-plus';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import 'element-plus/dist/index.css';
import 'nprogress/nprogress.css';

import App from './App.vue';
import router from './router';
import { setupSharedComponents } from '@platform/shared-components';
import { setupDirectives } from './directives';
import { setupPermission } from './permission';
import './styles/index.scss';

const app = createApp(App);
const pinia = createPinia();

app.use(pinia);
app.use(router);
app.use(ElementPlus, { locale: zhCn });

setupSharedComponents(app);
setupDirectives(app);
setupPermission(router);

app.mount('#app');
