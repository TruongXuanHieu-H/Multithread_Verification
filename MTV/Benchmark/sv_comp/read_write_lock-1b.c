/* Testcase from Threader's distribution. For details see:
   http://www.model.in.tum.de/~popeea/research/threader

   This file is adapted from the example introduced in the paper:
   Thread-Modular Verification for Shared-Memory Programs 
   by Cormac Flanagan, Stephen Freund, Shaz Qadeer.
*/

#include <pthread.h>
#include <stdio.h>
#include <assert.h>
#define assert(e) if (!(e)) ERROR: reach_error()


int w=0, r=0, x, y;
extern void abort(void);
void assume_abort_if_not(int cond) {
	//printf("Line 20: %d %d %d %d\n", w, r, x, y);
	if(!cond) {
		//printf("Line 22: Abort\n");
		abort();
	} else{
		//printf("Line 25: Not abort\n");
	}
}
extern void abort(void);
void reach_error() { 
	assert(0); 
}

void __VERIFIER_atomic_take_write_lock() {
	//printf("Line 35: %d %d %d %d\n", w, r, x, y);
	assume_abort_if_not(w==0 && r==0);
	//printf("Line 37: %d %d %d %d\n", w, r, x, y);
	w = 1;
	//printf("Line 39: %d %d %d %d\n", w, r, x, y);
} 

void __VERIFIER_atomic_take_read_lock() {
	//printf("Line 43: %d %d %d %d\n", w, r, x, y);
	assume_abort_if_not(w==0);
	//printf("Line 45: %d %d %d %d\n", w, r, x, y);
	r = r+1;
	//printf("Line 47: %d %d %d %d\n", w, r, x, y);
}

void __VERIFIER_atomic_release_read_lock() {
	//printf("Line 51: %d %d %d %d\n", w, r, x, y);
	r = r-1;
	//printf("Line 53: %d %d %d %d\n", w, r, x, y);
}

void *writer(void *arg) { //writer
	//printf("Line 57: %d %d %d %d\n", w, r, x, y);
	__VERIFIER_atomic_take_write_lock();  
	//printf("Line 59: %d %d %d %d\n", w, r, x, y);
	x = 3;
	//printf("Line 61: %d %d %d %d\n", w, r, x, y);
	w = 0; // w = 0
	//printf("Line 63: %d %d %d %d\n", w, r, x, y);
	return 0;
}

void *reader(void *arg) { //reader
	//printf("Line 68: %d %d %d %d\n", w, r, x, y);
	int l;
	//printf("Line 70: %d %d %d %d\n", w, r, x, y);
	__VERIFIER_atomic_take_read_lock();
	//printf("Line 72: %d %d %d %d\n", w, r, x, y);
	l = x;
	//printf("Line 74: %d %d %d %d\n", w, r, x, y);
	y = l;
	//printf("Line 76: %d %d %d %d\n", w, r, x, y);
	//assert(y == x);
	//printf("Line 78: %d %d %d %d\n", w, r, x, y);
	__VERIFIER_atomic_release_read_lock();
	//printf("Line 80: %d %d %d %d\n", w, r, x, y);
	return 0;
}

int main() {
  	pthread_t t1, t2, t3, t4;
  	pthread_create(&t1, 0, writer, 0);
  	pthread_create(&t2, 0, reader, 0);
 	pthread_create(&t3, 0, writer, 0);
  	pthread_create(&t4, 0, reader, 0);
  	pthread_join(t1, 0);
  	pthread_join(t2, 0);
  	pthread_join(t3, 0);
  	pthread_join(t4, 0);
  	return 0;
}

