#include <pthread.h>
int x = 1, y = 1, m = 0, n = 0;
void* thr1(void* args) {
	x = y + 1; 
    m = y; 
    x = x + 1; 

}

void* thr2(void* args) {
	y = x + 1; 
    n = x; 
    y = y + 1; 

}

void main() {
	x = y + 1;
	y = x + 2; 
	pthread_t t1, t2;
	pthread_create(&t1, 0, thr1, 0);
	pthread_create(&t2, 0, thr2, 0);
	pthread_join(t2, 0);
	
	x = y + 1;
	y = x + 2;
}
