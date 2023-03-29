#include <pthread.h>
#include "assert.h"

int x;
int n = 20;
int check_x;
void* thr1(void* arg) {
    check_x = x;
}

void* thr2(void* arg) {
    int t;
    t = x;
    x = t + 1;
}

int main(int argc, char* argv[]) {
    pthread_t t1, t2;
    x = 0;
    
    
	pthread_create(&t1, 0, thr1, 0);
    
	pthread_create(&t2, 0, thr2, 0);
	pthread_create(&t2, 0, thr2, 0);
	pthread_create(&t2, 0, thr2, 0);
	pthread_create(&t2, 0, thr2, 0);
	pthread_create(&t2, 0, thr2, 0); 
	
	pthread_create(&t2, 0, thr2, 0);
	pthread_create(&t2, 0, thr2, 0);
	pthread_create(&t2, 0, thr2, 0);
	pthread_create(&t2, 0, thr2, 0);
	pthread_create(&t2, 0, thr2, 0); 
	
	
	pthread_create(&t2, 0, thr2, 0);
	pthread_create(&t2, 0, thr2, 0);
	pthread_create(&t2, 0, thr2, 0);
	pthread_create(&t2, 0, thr2, 0);
	pthread_create(&t2, 0, thr2, 0); 
	
	pthread_create(&t2, 0, thr2, 0);
	pthread_create(&t2, 0, thr2, 0);
	pthread_create(&t2, 0, thr2, 0);
	pthread_create(&t2, 0, thr2, 0);
	pthread_create(&t2, 0, thr2, 0);     
	
	
    return 0;
}

