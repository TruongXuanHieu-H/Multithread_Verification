#include <pthread.h>
int x = 1, y =2;
void* thr1(void* args) {
	x = y;
}

void* thr2(void* args) {
	y = x;
}

void main() {
	x = y + y;
	pthread_t t1, t2;
	pthread_create(&t1, 0, thr1, 0);
	pthread_create(&t2, 0, thr2, 0);
	pthread_join(t1, 0);
	x = y + 1;
}
