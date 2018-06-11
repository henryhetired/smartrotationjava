extern "C"
__global__ void calc_entropy_atomic(float *float_image_in, float *entropy_out, int* blk) {
	//TODO: CHECK INDEX FOR ENTROPY_OUT
	//calculate entropy of a block through a single thread
        int blk_size = &blk;
	__shared__ float sum;
	if (threadIdx.x == 0 && threadIdx.y == 0) {
		sum = 0.0;
	}
	__syncthreads();
	__shared__ float c;
	int blocksize = &blk_size*&blk_size;
	//vertical offset to get to beginning of own block
	int v_offset_to_blkrow = gridDim.x*blockDim.x*blockDim.y*blockIdx.y;
	int v_offset_to_pixrow = blockDim.x*gridDim.x*threadIdx.y;
	int h_offset = blockDim.x*blockIdx.x + threadIdx.x;
	int idx = v_offset_to_blkrow + v_offset_to_pixrow + h_offset; //idx of top left corner of the block
	int out_idx = blockIdx.y*gridDim.x + blockIdx.x;
	//normalize image
	float_image_in[idx] = float_image_in[idx] * float_image_in[idx] / (blocksize);
	atomicAdd(&sum, float_image_in[idx]);
	__syncthreads();
	__shared__ float entropy;
	if (threadIdx.x == 0 && threadIdx.y == 0) {
		entropy = 0.0;
	}
	__syncthreads();
	float_image_in[idx] = float_image_in[idx] / sum;
	//shannon entropy
	atomicAdd(&entropy, -float_image_in[idx] * log2(float_image_in[idx]));
	__syncthreads();
	//printf("%f\n", sum2);
	if (threadIdx.x == 0 && threadIdx.y == 0) {
		entropy_out[out_idx] = entropy;
	}
}
__global__ void thread_dct_h(float *float_image_in, float *coefficients, float *float_image_out, int* blk) {
	//dct on rows
	//summation using Kahan algorithm, very important!
  int blk_size = &blk;
	float sum = 0.0;
	float c = 0.0;
	for (int i = 0; i<blk_size; i++) {
		//printf("executing %d th task",i);

		//printf("param1 =  %d,param2 = %d,param3 = %d \n",gridDim.x*blockIdx.y*blockDim.x*blockDim.y+threadIdx.y*gridDim.x*blockDim.x+threadIdx.x+blockIdx.x*blockDim.x,threadIdx.y*blk_size+i,gridDim.x*blockIdx.y*blockDim.x*blockDim.y+threadIdx.y*gridDim.x*blockDim.x+threadIdx.x+blockIdx.x*blockDim.x+i);
		float temp = coefficients[threadIdx.y*blk_size + i] * float_image_in[gridDim.x*blockIdx.y*blockDim.x*blockDim.y + threadIdx.y*gridDim.x*blockDim.x + threadIdx.x + blockIdx.x*blockDim.x + i] - c;
		float t = sum + temp;
		c = (t - sum) - temp;
		sum = t;
	}
	float_image_out[gridDim.x*blockIdx.y*blockDim.x*blockDim.y + threadIdx.y*gridDim.x*blockDim.x + threadIdx.x + blockIdx.x*blockDim.x] = sum;
}
__global__ void thread_dct_v(float *float_image_in, float *coefficients, float *float_image_out, int* blk) {
	// dct on columns
	//summation using Kahan algorithm, very important!
  int blk_size = &blk;
	float sum = 0.0;
	float c = 0.0;
	for (int i = 0; i<blk_size; i++) {
		//printf("executing %d th task",i);

		//printf("param1 =  %d,param2 = %d,param3 = %d \n",gridDim.x*blockIdx.y*blockDim.x*blockDim.y+threadIdx.y*gridDim.x*blockDim.x+threadIdx.x+blockIdx.x*blockDim.x,threadIdx.y*blk_size+i,gridDim.x*blockIdx.y*blockDim.x*blockDim.y+threadIdx.y*gridDim.x*blockDim.x+threadIdx.x+blockIdx.x*blockDim.x+i);
		float temp = coefficients[threadIdx.y*blk_size + i] * float_image_in[gridDim.x*blockIdx.y*blockDim.x*blockDim.y + i*gridDim.x*blockDim.x + blockIdx.x*blockDim.x + threadIdx.x] - c;
		float t = sum + temp;
		c = (t - sum) - temp;
		sum = t;
	}
	float_image_out[gridDim.x*blockIdx.y*blockDim.x*blockDim.y + threadIdx.y*gridDim.x*blockDim.x + threadIdx.x + blockIdx.x*blockDim.x] = sum;
}
