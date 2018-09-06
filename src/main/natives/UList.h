#ifndef __UPDATE_LIST__
#define __UPDATE_LIST__

#include <jni.h>
#include <stdlib.h>

struct UListNode {
    struct UListNode *next, *prev;
    struct AudioSource *audioSource;
};

struct {
    struct UListNode *head, *tail;
} UList;

/**
 * Initializes the U-List.
 */

void ulistInit();

/**
 * Add a given node to the U-List.
 */

void ulistAdd(struct UListNode*);

/**
 * Remove a given node from the U-List.
 */

void ulistRemove(struct UListNode*);

/**
 * Check if a given node is the U-List's tail comparing its address to the tail's one.
 */

jboolean ulistIsTail(struct UListNode*);

#endif
