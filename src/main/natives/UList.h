/**
* Copyright (c) 2018, Riccardo Balbo - Lorenzo Catania
* All rights reserved.
*
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*
* - Redistributions of source code must retain the above copyright
*      notice, this list of conditions and the following disclaimer.
*
* - Redistributions in binary form must reproduce the above copyright
*      notice, this list of conditions and the following disclaimer in the
*      documentation and/or other materials provided with the distribution.
*
* - Neither the name of the developers nor the
*      names of the contributors may be used to endorse or promote products
*      derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
* ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
* WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
* LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
*/
#ifndef __UPDATE_LIST__
#define __UPDATE_LIST__

#include "Common.h" 


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

struct UListNode* ulistInitNode(struct UListNode*, struct AudioSource*);

/**
 * Add a given node to the U-List.
 */

void ulistAdd(struct UList*, struct UListNode*);

/**
 * Remove a given node from the U-List.
 */

void ulistRemove(struct UListNode*);

/**
 * Check if the given node is the given U-List's tail comparing its address to the tail's one.
 */

jboolean ulistIsTail(struct UList*, struct UListNode*);

/**
 * Destroy a U-List.
 */

void ulistDestroy(struct UList*);

/**
 * Destroy a U-List node.
 */

void ulistDestroyNode(struct UListNode*);

#endif
