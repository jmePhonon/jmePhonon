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
#include "UList.h"

void ulistInit(struct UList* uList) {
    uList->head = (struct UListNode*) malloc(sizeof(struct UListNode));
    uList->tail = (struct UListNode*) malloc(sizeof(struct UListNode));
    
    uList->head->next = uList->tail;
    uList->head->prev = NULL;
    
    uList->tail->next = NULL;
    uList->tail->prev = uList->head;
}

struct UListNode* ulistInitNode(struct UListNode* node, struct AudioSource* source) {
    node->audioSource = source;
    node->next = NULL;
    node->prev = NULL;
    node->connected = false;

    return node;
}

void ulistAdd(struct UList* uList, struct UListNode* node) {
    node->prev = uList->tail->prev;
    uList->tail->prev->next = node;

    uList->tail->prev = node;
    node->next = uList->tail;

    node->connected = true;
}

void ulistRemove(struct UListNode* node) {
    node->prev->next = node->next;
    node->next->prev = node->prev;

    node->connected = false;

    // node->next = NULL;
    // node->prev = NULL;
}

jboolean ulistIsTail(struct UList* uList, struct UListNode* node) {
    return node == uList->tail;
}

void ulistDestroy(struct UList* uList) {
    ulistDestroyNode(uList->head);

    free(uList);
}

void ulistDestroyNode(struct UListNode* node) {
    if(node->next != NULL) {
        ulistDestroyNode(node->next);
    }

    free(node);
}