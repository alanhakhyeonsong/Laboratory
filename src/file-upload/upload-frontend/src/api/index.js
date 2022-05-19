import axios from "axios";

const instance = axios.create({
    baseURL: 'http://localhost:8080',
});

function upload(data) {
    return instance.post('/rest/upload', data, {
        headers: {
            'Content-Type': 'multipart/form-data',
            'Access-Control-Allow-Origin': '*',
        }
    });
}

export { upload }