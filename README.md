# DSPS Task 2 - Map Reduce

Google 2-gram

<p align="center">
  <a href="#dsps-task-2---map-reduce"><img src="https://miro.medium.com/max/4000/1*b_al7C5p26tbZG4sy-CWqw.png" width="350" title="AWS" target="_blank"/></a>
</p>

## Map Reduce model

The input is from the **[Google 2-gram](http://storage.googleapis.com/books/ngrams/books/datasetsv2.html)** dataset: `ngram TAB year TAB match_count TAB volume_count NEWLINE`  
Good ref: [Click Here](https://github.com/MaorRocky/Collocation-Extraction-Amazon-EMR)

N           = total number of bi-grams in the corpus (number of rows)  
c(w1, w2)   = count of the bigram w1w2 in a decade  
c(w1)       = count of bigrams that starts with w1 in the entire corpus  
c(w2)       = count of bigrams that ends with w2 in the entire corpus  

## Round 1

In the first round we want to compute N, c(w1) and c(w1,w2)

### Map:
__input:__ "w1_w2 year occurrences ?? ??"  
__output 1:__ K = decade w1 w2, V = occurrences  
__output 2:__ K = decade w1 *, V = occurrences  
__output 3:__ K = decade * w2, V = occurrences  

### Shuffle and Sort:
Distribute the K-V formed according to decades they're in  
Sort them according to w1 and then w2 (gram containing * are before everything)

### Reduce:
__input 1:__ K = decade w1 *,  V = [occ1, occ2, ...]  
__input 2:__ K = decade * w2,  V = [occ1, occ2, ...]  
__input 3:__ K = decade w1 w2,  V = [occ1, occ2, ...]  
__output:__ `decade w1 w2 TAB c_w1_w2 c_w1 (or c_w2) N`

> At the end of this Round we have 10 files (5 for c_w1 and 5 for c_w2).

```text
1670 אבא בעיר	4 145540 522190510
1700 אבא עוד	1 145540 522190510
1750 אבא אבוה	2 145540 522190510
1750 אבא באהל	1 145540 522190510
1760 אבא ושלא	1 145540 522190510
1790 אבא אבוה	1 145540 522190510
1790 אבא וכו	1 145540 522190510
1790 אבא כתב	1 145540 522190510
1790 אבא מרי	3 145540 522190510
1790 אבא שמואל	1 145540 522190510
1800 אבא לבאר	1 145540 522190510
1800 אבא לביתך	1 145540 522190510
```

## Round 2

Regroupment of all parameters and Calculation of nmpi  

```LaTex
npmi(w1,w2)=\frac{pmi(w1,w2)}{-log[p(w1,w2)]}
pmi(w1,w2)=log[c(w1,w2)]+log(N)-log(c(w1))-log(c(w2))
```

### Map:
__input:__ "decade w1 w2 TAB c_w1_w2 c_w1 (or c_w2) N"  
__output:__ K = Gram2 Object, V = c_w1_w2 c_w1 (or c_w2) N  

> The mapper doesn't do anything but restructure the input 

### Shuffle and Sort:
According to if we want to calculate c_w1 or c_w2 we sort the mapper outputs
based on w1 or w2 (* are always prioritized)  
If same w1 or w2 then sort by decade

### Reduce:
__input:__ K = Gram2 Object,  V = [c_w1_w2 c_w1 N, c_w1_w2 c_w2 N]  
__output:__ `decade w1 w2 TAB npmi`

> At the end of this Round we have 5 files regrouping all the corpus npmi's

## Round 3

Compute the relative npmi and filter out not high enough values

```LaTex
rel_npmi(w1,w2)=\frac{npmi(w1,w2)}{\sum_{j} npmi_{j}}
```

```text
Filter out if: npmi < minPmi OR rel_npmi < relMinPmi
```

### Map:
__input:__ "decade w1 w2 TAB npmi"  
__output 1:__ K = decade w1 w2, V = npmi  
__output 2:__ K = decade *  *, V = npmi

> In order to compute the relative npmi we need to find 

### Shuffle and Sort:
No need to shuffle -> only 1 reducer
Sort according to Decade then W1 and finally W2

### Reduce:
__input 1:__ K = decade *  *, V = npmi  
__input 2:__ K = decade w1 w2, V = [npmi_1, npmi_2, ...]  
__output:__ `decade w1 w2 TAB Normalized PMI = npmi(w1,w2)`
