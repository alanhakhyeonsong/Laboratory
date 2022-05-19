<template>
  <form @submit.prevent="submit">
    <div>
      <label for="uploadfile">Upload</label>
      <input name="uploadfile" id="uploadfile" type="file" ref="uploadfile" />
    </div>
    <button type="submit">submit</button>
  </form>
  <p>{{ resultMessage }}</p>
</template>

<script>
import { upload } from '@/api/index';

export default {
  name: "UploadForm",
  data() {
    return {
      uploadFile: '',
      resultMessage: '',
    };
  },
  methods: {
    async submit() {
      const formData = new FormData();
      formData.append('uploadFile', this.$refs['uploadfile'].files[0]);
      try {
        await upload(formData);
        this.resultMessage = "success";
      } catch (error) {
        console.log(error);
        this.resultMessage = error;
      }
    }
  },
}
</script>

<style scoped>

</style>