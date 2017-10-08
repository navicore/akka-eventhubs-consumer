deploy on k8s:
---

1. edit controller.yaml env vars with Azure info
2. `kubectl create -f ./service.yaml`
3. `kubectl create -f ./controller.yaml`

deploy on k8s with Istio:
---

1. edit controller.yaml env vars with Azure info
2. `kubectl create -f ./service.yaml`
3. `kubectl create -f ./ingress.yaml`
4. `kubectl apply -f <(istioctl kube-inject -f ./controller.yaml --includeIPRanges=10.0.0.0/8)`

* you may have to restart the ingress pod to get it to use the new path
* the `includeRanges` should reflect your local CIDR(s) - this is just until I figure out how to get the Azure APIs to not use https so that the sidecar can initiate https - to use sidecar engress on https, you must use the equiv of `http://blah.microsoft.com:443` rather than `https://`.
