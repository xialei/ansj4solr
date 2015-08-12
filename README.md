ansj4solr
=========

solr的ansj分词插件，支持5以上

[ansj分词](https://github.com/ansjsun/ansj_seg)是一个基于google语义模型+条件随机场模型的中文分词的java实现.。

也可以执行mvn assembly:assembly 把zip里的三个包拿出来。

[ik分词插件](https://github.com/lgnlgn/ik4solr4.3), 以及动态[停用词、同义词插件](https://github.com/lgnlgn/stop4solr4.x)包


配置如下
=========

在schema.xml中配置tokenizerfactory

     <fieldType name="text_cn" class="solr.TextField" positionIncrementGap="100">
     <analyzer type="index">
       <tokenizer class="org.ansj.solr.AnsjTokenizerFactory" conf="ansj.conf"/>
     </analyzer>
    	 <analyzer type="query">
       <tokenizer class="org.ansj.solr.AnsjTokenizerFactory" analysisType="1"/>
     </analyzer>
       </fieldType>


说明一下： 

1.

conf="ansj.conf" 这个tokenizerfactory需要的配置，里面是个properties格式的配置：

    lastupdate=123
    files=extDic.txt,aaa.txt

其中lastupdate 是一个数字，只要这次比上一次大就会触发更新操作，可以用时间戳 files是用户词库文件，以**英文逗号**隔开

conf配置只要在一个地方配置上了，整个索引使用的ansj都会启用定时更新功能，切词库是schema内共享的。这里和IK的设置是一致的。

2.

analysisType="1" 表示分词类型，1代表标准切分，不写默认是0。是索引类型切分（也就是可能多分出一点词）。

3.

rmPunc="false" 表示不去除标点符号。默认不写是true，表示去掉标点符号。
