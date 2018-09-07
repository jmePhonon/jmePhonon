#ifndef __UPDATE_LIST__
#define __UPDATE_LIST__

#include <jni.h>
#include <stdlib.h>
#include "types.h"

struct UListNode {
    struct UListNode *next, *prev;
    struct AudioSource *audioSource;
    jboolean connected;
};

struct UList {
    struct UListNode *head, *tail;
};

/**
 * Initializes the given U-List.
 */

void ulistInit(struct UList*);

/**
 * Creates a new U-List node.
 */

struct UListNode* ulistCreateNode(struct AudioSource*);

/**
 * Add a given node to the U-List.
 */

void ulistAdd(struct UList*, struct UListNode*);

/**
 * Remove a given node from the U-List.
 */

void ulistRemove(struct UListNode*);

/**
 * Check if a given node is the U-List's tail comparing its address to the tail's one.
 */

jboolean ulistIsTail(struct UList*, struct UListNode*);

#endif
