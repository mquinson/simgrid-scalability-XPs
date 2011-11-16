
oarsub -I -l 'nodes=1,walltime=1' -p "cluster='graphene'" -t deploy 
kadeploy3 -e squeeze-x64-nfs -f $OAR_NODE_FILE 
ssh `head -1 $OAR_NODE_FILE`
