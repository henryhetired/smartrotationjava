library(ggplot2)
library(ggridges)
workspace <- "/mnt/fileserver/Henry-SPIM/smart_rotation/11222018/e5/data/workspace/"
for (angle in 0:31){
  df <- read.csv(file=sprintf(paste(workspace,"distribution%02d.csv",sep=""),angle),header=TRUE,sep=',')
  p = ggplot(df,aes(x=Imaging.angle,y= Angular.slices,height=Number,group=Angular.slices))+geom_ridgeline(alpha=0.1,scale=0.003,fill="white",show.legend=TRUE)+labs(x=expression(paste("Imaging angles /",degree)),y=expression(paste("Angular slices /",degree)))+theme_ridges()  
  ggsave(filename=sprintf("/mnt/fileserver/Henry-SPIM/smart_rotation/11222018/e5/data/workspace/figures/rainbow/distribution%02d.pdf",angle),dpi = 500,width = 10,height = 7.5)
}
