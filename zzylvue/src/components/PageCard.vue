<template>
  <div class="page-card">
    <div v-if="$slots.filter" class="page-filter">
      <slot name="filter" />
    </div>
    <div v-if="$slots.toolbar" class="page-toolbar">
      <slot name="toolbar" />
    </div>
    <div class="page-body">
      <slot />
    </div>
    <div v-if="$slots.footer" class="page-footer">
      <slot name="footer" />
    </div>
  </div>
</template>

<style scoped>
.page-card {
  background: #fff;
  border-radius: 4px;
  padding: 20px;
  min-height: calc(100vh - 140px);
}
.page-filter {
  margin-bottom: 16px;
}
.page-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.page-footer {
  margin-top: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
