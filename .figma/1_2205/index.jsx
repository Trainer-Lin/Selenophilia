import React from 'react';

import styles from './index.module.scss';

const Component = () => {
  return (
    <div className={styles.row}>
      <div className={styles.title2}>
        <p className={styles.title}>Title</p>
        <div className={styles.container}>
          <img src="../image/mmjduki9-3lb9u2q.svg" className={styles.iconChevron} />
        </div>
      </div>
      <div className={styles.carousel}>
        <div className={styles.item}>
          <img src="../image/mmjdukia-h6mbhw5.png" className={styles.image} />
          <p className={styles.title3}>Title</p>
        </div>
        <div className={styles.item}>
          <img src="../image/mmjdukia-7uatkka.png" className={styles.image} />
          <p className={styles.title3}>Title</p>
        </div>
        <div className={styles.item}>
          <img src="../image/mmjdukia-uu27s4i.png" className={styles.image} />
          <p className={styles.title3}>Title</p>
        </div>
        <div className={styles.item}>
          <img src="../image/mmjdukia-sikdeis.png" className={styles.image} />
          <p className={styles.title3}>Title</p>
        </div>
      </div>
    </div>
  );
}

export default Component;
