// This file is part of the SV-Benchmarks collection of verification tasks:
// https://github.com/sosy-lab/sv-benchmarks
//
// SPDX-FileCopyrightText: 2011-2020 The SV-Benchmarks community
// SPDX-FileCopyrightText: 2018 Dirk Beyer <https://www.sosy-lab.org>
//
// SPDX-License-Identifier: Apache-2.0

#include <pthread.h>

extern void abort(void);
#include <assert.h>
void reach_error() { assert(0); }

int i = 3, j = 6;

int LIMIT = 16;

void *t1(void *arg) {
	int k;
  for (k = 0; k < NUM; k++) {
    i = j + 1;
  }
  printf("Terminate t1\n");
  pthread_exit(NULL);
}

void *t2(void *arg) {
	int k;
  for (k = 0; k < NUM; k++) {
    j = i + 1;
  }
  printf("Terminate t2\n");
  pthread_exit(NULL);
}

int main(int argc, char **argv) {
  pthread_t id1, id2;

  pthread_create(&id1, NULL, t1, NULL);
  pthread_create(&id2, NULL, t2, NULL);

  int condI = i > LIMIT;

  int condJ = j > LIMIT;

  if (condI || condJ) {
    ERROR: {reach_error();abort();}
  }

  return 0;
}

