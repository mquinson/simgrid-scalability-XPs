#cfgs="      v361_ctx     v361_raw     v361_pth   "
cfgs="$cfgs v361_ctx_12k v361_raw_12k v361_pth_12k "
cfgs="$cfgs v35_ctx      v35_pth v361_pth";

for arg in $cfgs ; do

   ./test_scalability.sh $arg

done
