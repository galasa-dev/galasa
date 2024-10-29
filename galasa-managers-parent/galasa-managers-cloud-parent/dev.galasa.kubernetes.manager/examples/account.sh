#!/bin/sh

#
# Copyright contributors to the Galasa project
#
# SPDX-License-Identifier: EPL-2.0
#

kubectl -n galasa1 create serviceaccount galasa

kubectl -n galasa1 get serviceaccounts galasa -o yaml

echo "Look at the secret to obtain the access token"