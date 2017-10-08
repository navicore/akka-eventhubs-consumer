deploy on k8s:

1. edit controller.yaml env vars with Azure info
2. kubectl create -f ./service.yaml
3. kubectl create -f ./controller.yaml

deploy on k8s with Istio:

1. edit controller.yaml env vars with Azure info
1. kubectl create -f .
  * you may have to restart the ingress pod to get it to use the new path
