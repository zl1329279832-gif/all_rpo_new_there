import type { App } from 'vue';

export * from './PageContainer';
export * from './SearchForm';
export * from './DataTable';
export * from './StatusTag';
export * from './ExceptionPage';

import PageContainer from './PageContainer.vue';
import SearchForm from './SearchForm.vue';
import DataTable from './DataTable.vue';
import StatusTag from './StatusTag.vue';
import ExceptionPage from './ExceptionPage.vue';

export function setupSharedComponents(app: App): void {
  app.component('PageContainer', PageContainer);
  app.component('SearchForm', SearchForm);
  app.component('DataTable', DataTable);
  app.component('StatusTag', StatusTag);
  app.component('ExceptionPage', ExceptionPage);
}
